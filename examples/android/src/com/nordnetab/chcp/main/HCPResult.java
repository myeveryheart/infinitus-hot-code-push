package com.nordnetab.chcp.main;

import com.nordnetab.chcp.main.model.ChcpError;

/**
 * Created by M on 16/9/9.
 */
abstract class HCPResult {

    public abstract void fetchUpdateResult(boolean needUpdate, ChcpError error);
}
