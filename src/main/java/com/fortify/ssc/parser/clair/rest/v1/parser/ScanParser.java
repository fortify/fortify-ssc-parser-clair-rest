package com.fortify.ssc.parser.clair.rest.v1.parser;

import java.io.IOException;
import java.util.Date;

import com.fortify.plugin.api.ScanBuilder;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.util.ssc.parser.ScanDataStreamingJsonParser;

public class ScanParser {
	private final ScanData scanData;
    private final ScanBuilder scanBuilder;
    
	public ScanParser(final ScanData scanData, final ScanBuilder scanBuilder) {
		this.scanData = scanData;
		this.scanBuilder = scanBuilder;
	}
	
	public final void parse() throws ScanParsingException, IOException {
		new ScanDataStreamingJsonParser()
			.handler("/Layer/Name", jp -> scanBuilder.setBuildId(jp.getValueAsString()))
			.parse(scanData);
		scanBuilder.setScanDate(new Date()); // Required but not available in input
		scanBuilder.completeScan();
	}
}
