package com.nordnetab.chcp.main;

import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/13.
 */
public interface HCPResult {

    void fetchUpdateResult(boolean needUpdate, ChcpError error);
}