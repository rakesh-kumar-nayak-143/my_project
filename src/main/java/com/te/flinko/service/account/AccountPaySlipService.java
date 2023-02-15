package com.te.flinko.service.account;

import java.util.ArrayList;

import com.te.flinko.dto.account.AccountPaySlipInputDTO;
import com.te.flinko.dto.account.AccountPaySlipListDTO;

public interface AccountPaySlipService {

	ArrayList<AccountPaySlipListDTO> paySlip(AccountPaySlipInputDTO accountPaySlipInputDTO);

}
