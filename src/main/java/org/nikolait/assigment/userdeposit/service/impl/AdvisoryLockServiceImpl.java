package org.nikolait.assigment.userdeposit.service.impl;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.nikolait.assigment.userdeposit.emum.AdvisoryLockType;
import org.nikolait.assigment.userdeposit.service.AdvisoryLockService;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisoryLockServiceImpl implements AdvisoryLockService {

    private final DataSource dataSource;
    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();
    private final Set<AdvisoryLockType> acquiredLocks = ConcurrentHashMap.newKeySet();

    @Override
    public boolean tryAcquirePermanentLock(AdvisoryLockType lockType) {
        if (acquiredLocks.contains(lockType)) {
            return true;
        }
        try {
            Connection connection = connectionRef.updateAndGet(c -> getConnection(c, lockType));
            if (tryAdvisoryLock(lockType.getKey(), connection)) {
                acquiredLocks.add(lockType);
                log.info("Lock {} acquired", lockType);
                return true;
            }

            if (connection == null) {
                return false;
            }

            log.debug("Lock {} not acquired", lockType);
            return false;

        } catch (SQLException e) {
            log.error("Failed to acquire lock {}", lockType, e);
            return false;
        }
    }

    private Connection getConnection(Connection c, AdvisoryLockType lockType) {
        try {
            if (c == null || c.isClosed()) {
                Connection connection = dataSource.getConnection();
                connection.setAutoCommit(true);
                return connection;
            }
        } catch (SQLException e) {
            log.error("Failed to open connection for lock {}", lockType, e);
            return null;
        }
        return c;
    }

    private boolean tryAdvisoryLock(long lockKey, Connection connection) throws SQLException {
        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT pg_try_advisory_lock(?)")
        ) {
            stmt.setLong(1, lockKey);
            ResultSet rs = stmt.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        }
    }

    @PreDestroy
    public void close() {
        Connection connection = connectionRef.getAndSet(null);
        if (connection != null) {
            try {
                connection.close();
                log.info("Connection closed, {} locks released", acquiredLocks.size());
            } catch (SQLException e) {
                log.error("Error closing connection", e);
            } finally {
                acquiredLocks.clear();
            }
        }
    }
}
