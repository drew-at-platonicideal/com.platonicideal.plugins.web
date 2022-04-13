package com.platonicideal.plugins;

import java.util.List;

public interface ReportTable<RecordType> {

    long getRecordsTotal();
    long getRecordsFiltered();
    List<RecordType> getData();
    RecordType getFooter();
	
}
