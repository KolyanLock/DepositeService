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
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdvisoryLockServiceImpl implements AdvisoryLockService {

    private final DataSource dataSource;
    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();

    @Override
    public boolean tryLock(AdvisoryLockType lockType) {
        try {
            Connection connection = connectionRef.updateAndGet(c -> getConnection(c, lockType));

            if (connection == null) {
                return false;
            }

            if (tryAdvisoryLock(lockType.getKey(), connection)) {
                log.info("Lock {} acquired", lockType);
                return true;
            }

            log.debug("Lock {} not acquired", lockType);
            return false;

        } catch (SQLException e) {
            log.error("Failed to acquire lock {}", lockType, e);
            return false;
        }
    }

    @Override
    public void releaseLock(AdvisoryLockType lockType) {
        Connection connection = connectionRef.get();
        if (connection == null) {
            log.warn("Connection is null while releasing lock {}", lockType);
            return;
        }
        releaseAdvisoryLock(lockType, connection);
    }

    private Connection getConnection(Connection c, AdvisoryLockType lockType) {
        try {
            if (c == null || c.isClosed()) {
                return dataSource.getConnection();
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

    private void releaseAdvisoryLock(AdvisoryLockType lockType, Connection connection) {
        try (PreparedStatement ps = connection.prepareStatement(
                "SELECT pg_advisory_unlock(?)"
        )) {
            ps.setLong(1, lockType.getKey());
            ps.execute();
            log.info("Lock {} released", lockType);
        } catch (SQLException e) {
            log.error("Failed to release lock {}", lockType, e);
        }
    }

    @PreDestroy
    public void close() {
        Connection connection = connectionRef.getAndSet(null);
        if (connection != null) {
            try {
                connection.close();
                log.info("Connection closed, locks released");
            } catch (SQLException e) {
                log.error("Error closing connection", e);
            }
        }
    }
}
