package org.nikolait.assigment.userdeposit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.nikolait.assigment.userdeposit.validation.Phone;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PhoneRequest {

    @Phone
    String phone;

}
