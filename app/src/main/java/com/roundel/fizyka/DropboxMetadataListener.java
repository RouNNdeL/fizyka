package com.roundel.fizyka;

import java.util.List;

/**
 * Created by RouNdeL on 2016-09-25.
 */
public interface DropboxMetadataListener
{
    void onTaskStart();
    void onTaskEnd(List<String> result);
}
