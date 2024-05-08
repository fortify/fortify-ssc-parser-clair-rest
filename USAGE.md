
<!-- START-INCLUDE:repo-usage.md -->


<!-- START-INCLUDE:usage/h1.standard-parser-usage.md -->

<x-tag-head>
<x-tag-meta http-equiv="X-UA-Compatible" content="IE=edge"/>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/highlight.min.js"/>
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js" />
--></x-tag-script>

<x-tag-script language="JavaScript"><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.js" />
--></x-tag-script>

<x-tag-style><!--
<X-INCLUDE url="https://cdn.jsdelivr.net/gh/highlightjs/cdn-release@10.0.0/build/styles/github.min.css" />
--></x-tag-style>

<x-tag-style><!--
<X-INCLUDE url="${gradleHelpersLocation}/spa_readme.css" />
--></x-tag-style>
</x-tag-head>

# Fortify SSC Parser Plugin for Clair (REST API) - Usage

## Introduction


<!-- START-INCLUDE:p.marketing-intro.md -->

[Fortify Application Security](https://www.microfocus.com/en-us/solutions/application-security) provides your team with solutions to empower [DevSecOps](https://www.microfocus.com/en-us/cyberres/use-cases/devsecops) practices, enable [cloud transformation](https://www.microfocus.com/en-us/cyberres/use-cases/cloud-transformation), and secure your [software supply chain](https://www.microfocus.com/en-us/cyberres/use-cases/securing-the-software-supply-chain). As the sole Code Security solution with over two decades of expertise and acknowledged as a market leader by all major analysts, Fortify delivers the most adaptable, precise, and scalable AppSec platform available, supporting the breadth of tech you use and integrated into your preferred toolchain. We firmly believe that your great code [demands great security](https://www.microfocus.com/cyberres/application-security/developer-security), and with Fortify, go beyond 'check the box' security to achieve that.

<!-- END-INCLUDE:p.marketing-intro.md -->



<!-- START-INCLUDE:repo-intro.md -->

This Fortify SSC parser plugin allows for importing scan results from Clair (Vulnerability Static Analysis for Containers).

Clair itself doesn't provide any file-based reports; as such this parser plugin parses files containing JSON produced by the
Clair 2.x `/v1/layers/{layerId}?features&vulnerabilities` REST API call

<!-- END-INCLUDE:repo-intro.md -->


## Plugin Installation

These sections describe how to install, upgrade and uninstall the parser plugin in SSC.

### Install & Upgrade

* Obtain the plugin binary jar file; either:
     * Download from the repository release page: https://github.com/fortify/fortify-ssc-parser-clair-rest/releases
     * Build the plugin from source: https://github.com/fortify/fortify-ssc-parser-clair-rest/CONTRIB.md
* If you already have another version of the plugin installed, first uninstall the previously  installed version of the plugin by following the steps under [Uninstall](#uninstall) below
* In Fortify Software Security Center:
	* Navigate to Administration->Plugins->Parsers
	* Click the `NEW` button
	* Accept the warning
	* Upload the plugin jar file
	* Enable the plugin by clicking the `ENABLE` button
  
### Uninstall

* In Fortify Software Security Center:
     * Navigate to Administration->Plugins->Parsers
     * Select the parser plugin that you want to uninstall
     * Click the `DISABLE` button
     * Click the `REMOVE` button 

## Obtain results


<!-- START-INCLUDE:parser-obtain-results.md -->

* Have Clair perform a scan of your container image
	* For example, using some Clair command line client like Yair
	* Or through container registry integration
* Determine the bottom layer id of the container image that was scanned
	* For example by inspecting the image manifest
* Invoke the Clair `/v1/layers/{layerId}?features&vulnerabilities` REST endpoint
	* Replace `{layerId}` with the bottom layer id identified in the previous step
	* Save the results in a file with the `.json` extension
	* See https://coreos.com/clair/docs/latest/api_v1.html#get-layersname for more information about this API endpoint
	* According to the documentation, this REST endpoint returns all vulnerabilities for both the given layer, and all upper layers
    
The following steps were used to generate the 
[sampleData/node_10.14.2-jessie.clair.rest.json](sampleData/node_10.14.2-jessie.clair.rest.json) 
file:

* Use Yair to scan the `node:10.14.2-jessie` image
	* See https://github.com/fortify-ps/fortify-ssc-parser-clair-yair#obtain-results for an example on how to set-up Clair and run a scan with Yair
* Use the following command to determine the bottom layer id:  
  `layerId=$(docker manifest inspect -v node:10.14.2-jessie | jq -r '.[0]["SchemaV2Manifest"]["layers"][-1]["digest"]')`
	* This command requires Docker experimental mode to be enabled
	* Requires `jq` to be installed
	* Other images may require slightly different approach, depending on manifest version
	* Potentially there are better ways of obtaining this information
* Use the following command to invoke the Clair REST API endpoint and save the results:  
  `curl -X GET "http://localhost:6060/v1/layers/${layerId}?features&vulnerabilities" -o  node_10.14.2-jessie.clair.rest.json`

<!-- END-INCLUDE:parser-obtain-results.md -->


## Upload results

Results can be uploaded through the SSC web interface, REST API, or SSC client utilities like FortifyClient or [fcli](https://github.com/fortify-ps/fcli). The SSC web interface, FortifyClient and most other Fortify clients require the raw results to be packaged into a zip-file; REST API and fcli allow for uploading raw results directly.

To upload results through the SSC web interface or most clients:

* Create a `scan.info` file containing a single line as follows:   
     `engineType=CLAIR_REST_V1`
* Create a zip file containing the following:
	* The scan.info file generated in the previous step
	* The raw results file as obtained from the target system (see [Obtain results](#obtain-results) section above)
* Upload the zip file generated in the previous step to SSC
	* Using any SSC client, for example FortifyClient or Maven plugin
	* Or using the SSC web interface
	* Similar to how you would upload an FPR file
	
Both SSC REST API and fcli provide options for specifying the engine type directly, and as such it is not necessary to package the raw results into a zip-file with accompanying `scan.info` file. For example, fcli allows for uploading raw scan results using a command like the following:

`fcli ssc artifact upload -f <raw-results-file> --appversion MyApp:MyVersion --engine-type CLAIR_REST_V1`

<!-- END-INCLUDE:usage/h1.standard-parser-usage.md -->


<!-- END-INCLUDE:repo-usage.md -->


---

*[This document was auto-generated from USAGE.template.md; do not edit by hand](https://github.com/fortify/shared-doc-resources/blob/main/USAGE.md)*
