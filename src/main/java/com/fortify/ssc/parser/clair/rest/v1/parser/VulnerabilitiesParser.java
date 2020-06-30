package com.fortify.ssc.parser.clair.rest.v1.parser;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonToken;
import com.fortify.plugin.api.BasicVulnerabilityBuilder.Priority;
import com.fortify.plugin.api.FortifyAnalyser;
import com.fortify.plugin.api.FortifyKingdom;
import com.fortify.plugin.api.ScanData;
import com.fortify.plugin.api.ScanParsingException;
import com.fortify.plugin.api.StaticVulnerabilityBuilder;
import com.fortify.plugin.api.VulnerabilityHandler;
import com.fortify.ssc.parser.clair.rest.v1.CustomVulnAttribute;
import com.fortify.ssc.parser.clair.rest.v1.domain.Feature;
import com.fortify.ssc.parser.clair.rest.v1.domain.FeatureWithVulnerabilities;
import com.fortify.ssc.parser.clair.rest.v1.domain.Vulnerability;
import com.fortify.util.ssc.parser.EngineTypeHelper;
import com.fortify.util.ssc.parser.HandleDuplicateIdVulnerabilityHandler;
import com.fortify.util.ssc.parser.json.ScanDataStreamingJsonParser;

public class VulnerabilitiesParser {
	private static final String ENGINE_TYPE = EngineTypeHelper.getEngineType();
	@SuppressWarnings("serial")
	private static final Map<String,Priority> CVE_SEVERITY_TO_PRIORITY_MAP = new HashMap<String, Priority>() {{
		put("Unknown", Priority.Medium);
		put("Negligible", Priority.Low);
		put("Low", Priority.Low);
		put("Medium", Priority.Medium);
		put("High", Priority.High);
		put("Critical", Priority.Critical);
		put("Defcon1", Priority.Critical);
	}};
	
	private final ScanData scanData;
	private final VulnerabilityHandler vulnerabilityHandler;

    public VulnerabilitiesParser(final ScanData scanData, final VulnerabilityHandler vulnerabilityHandler) {
    	this.scanData = scanData;
		this.vulnerabilityHandler = new HandleDuplicateIdVulnerabilityHandler(vulnerabilityHandler);
	}
    
    /**
	 * Main method to commence parsing the input provided by the configured {@link ScanData}.
	 * @throws ScanParsingException
	 * @throws IOException
	 */
	public final void parse() throws ScanParsingException, IOException {
		new ScanDataStreamingJsonParser()
			.expectedStartTokens(JsonToken.START_OBJECT)
			.handler("/Layer/Features/*", FeatureWithVulnerabilities.class, this::buildVulnerabilitiesForFeature)
			.parse(scanData);
	}
	
	/**
	 * Call the {@link #buildVulnerabilityIfValid(Feature, Vulnerability)} for each 
	 * {@link Vulnerability} contained in the given {@link FeatureWithVulnerabilities}
	 * instance (if any).
	 */
	private final void buildVulnerabilitiesForFeature(FeatureWithVulnerabilities feature) {
		Vulnerability[] vulnerabilities = feature.getVulnerabilities();
		if ( vulnerabilities!=null && vulnerabilities.length>0 ) {
			for ( Vulnerability vulnerability : vulnerabilities ) {
				buildVulnerabilityIfValid(feature, vulnerability);
			}
		}
	}
	
	/**
	 * Call the {@link #buildVulnerability(Feature, Vulnerability)} method if the 
	 * given {@link Vulnerability} provides valid vulnerability data.
	 * @param finding
	 */
	private void buildVulnerabilityIfValid(Feature feature, Vulnerability vuln) {
		if ( StringUtils.isNotBlank(vuln.getName()) ) {
			buildVulnerability(feature, vuln);
		}
	}

	/**
	 * Build the vulnerability from the given {@link Feature} and {@link Vulnerability}, 
	 * using the configured {@link VulnerabilityHandler}.
	 */
	private final void buildVulnerability(Feature feature, Vulnerability vuln) {
		StaticVulnerabilityBuilder vb = vulnerabilityHandler.startStaticVulnerability(getInstanceId(feature, vuln));
		
		// Set meta-data
		vb.setEngineType(ENGINE_TYPE);
		vb.setKingdom(FortifyKingdom.ENVIRONMENT.getKingdomName());
		vb.setAnalyzer(FortifyAnalyser.CONFIGURATION.getAnalyserName());
		vb.setCategory("Insecure Deployment");
		vb.setSubCategory("Unpatched Application");
		
		// Set mandatory values to JavaDoc-recommended values
		vb.setAccuracy(5.0f);
		vb.setConfidence(2.5f);
		vb.setLikelihood(2.5f);
		
		// Set standard vulnerability fields based on input
		vb.setFileName(feature.getName());
		vb.setPriority(CVE_SEVERITY_TO_PRIORITY_MAP.getOrDefault(vuln.getSeverity(), Priority.Medium));
		vb.setVulnerabilityAbstract(vuln.getDescription());
		
		// Set custom attributes based on input
		vb.setStringCustomAttributeValue(CustomVulnAttribute.FeatureName, feature.getName());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.FeatureNamespaceName, feature.getNamespaceName());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.FeatureVersionFormat, feature.getVersionFormat());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.FeatureVersion, feature.getVersion());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.FeatureAddedBy, feature.getAddedBy());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.VulnerabilityName, vuln.getName());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.VulnerabilityLink, vuln.getLink());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.VulnerabilitySeverity, vuln.getSeverity());
		vb.setStringCustomAttributeValue(CustomVulnAttribute.VulnerabilityFixedBy, vuln.getFixedBy());
		
		vb.completeVulnerability();
		
    }

	/**
	 * Calculate the issue instance id, using a combination of feature name, feature version, and vulnerability name
	 */
	private final String getInstanceId(Feature feature, Vulnerability vuln) {
		String featureName = StringUtils.defaultString(feature.getName(), "x");
		String featureVersion = StringUtils.defaultString(feature.getVersion(), "x");
		String vulnName = vuln.getName();
		return DigestUtils.sha256Hex(String.join("|", featureName, featureVersion, vulnName));
	}
}
