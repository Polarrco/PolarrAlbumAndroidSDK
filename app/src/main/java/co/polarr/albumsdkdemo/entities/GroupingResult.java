package co.polarr.albumsdkdemo.entities;

import java.util.ArrayList;
import java.util.List;

import co.polarr.processing.entities.ResultItem;

/**
 * Created by Colin on 2017/10/21.
 */

public class GroupingResult {
    public List<List<ResultItem>> optFiles = new ArrayList<>();
    public List<ResultItem> badFiles = new ArrayList<>();
    public List<ResultItem> droppedFiles = new ArrayList<>();
}
