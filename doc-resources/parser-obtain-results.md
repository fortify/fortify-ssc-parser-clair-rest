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