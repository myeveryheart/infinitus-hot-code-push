package com.nordnetab.hcp.main;

import com.nordnetab.hcp.main.model.HCPError;

/**
 * Created by M on 16/9/18.
 */
public interface HCPResult
{
    void fetchUpdateResult(boolean needUpdate, HCPError error);
}
