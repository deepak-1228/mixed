package com.stanzaliving.user.controller;

import com.stanzaliving.core.base.common.dto.ResponseDto;
import com.stanzaliving.user.dto.request.CashReconReceiverRequest;
import com.stanzaliving.user.dto.response.BankDetails;
import com.stanzaliving.user.dto.response.CashReconReceiverInfo;
import com.stanzaliving.user.dto.response.NodalOfficerInfo;
import com.stanzaliving.user.service.CashReconService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Log4j2
@RestController
@RequestMapping("cash-recon")
public class CashReconController {

    @Autowired
    private CashReconService cashReconService;

    @ApiOperation(value = "Cash Receiver List")
    @PostMapping("transfer/to/list")
    public ResponseDto<List<CashReconReceiverInfo>> getCashReceiverList(@RequestBody CashReconReceiverRequest cashReconReceiverRequest) {
        log.info("Received Cash receiver listing request. {}", cashReconReceiverRequest);
        try {
            return ResponseDto.success("Found Cash Receiver List", cashReconService.getReceiverList(cashReconReceiverRequest));
        }
        catch (Exception e){
            log.error("Error in getting cash-recon Receiver List for {} ", cashReconReceiverRequest.getUserUuid(), e);
            return ResponseDto.failure("Error in getting cash-recon Receiver List for : " + cashReconReceiverRequest.getUserUuid());
        }
    }

    @ApiOperation(value = "All nodal officers List")
    @GetMapping("nodal/officers/list")
    public ResponseDto<List<NodalOfficerInfo>> getNodalOfficersList() {
        log.info("Received all nodal officers listing request.");
        try {
            return ResponseDto.success("Found Nodal Officers List", cashReconService.getNodalOfficersList());
        }
        catch (Exception e){
            log.error("Error in getting all nodal officers List : ", e);
            return ResponseDto.failure("Error in getting all nodal officers List");
        }
    }
}
