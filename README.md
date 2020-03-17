# Fortify SSC Parser Plugin for Clair / REST API

* Travis-CI builds: https://travis-ci.com/fortify-ps/fortify-ssc-parser-clair-rest
* Binaries (sort by Updated column to find latest): https://bintray.com/beta/#/fortify-ps/binaries/fortify-ssc-parser-clair-rest?tab=files
* Sample JSON file: https://github.com/fortify-ps/fortify-ssc-parser-clair-rest/tree/1.0-SNAPSHOT/src/test/resources

This Fortify SSC parser plugin allows for importing Clair scan results from JSON output produced by the Clair /v1/layers/{layerId}?features&vulnerabilities REST API call. See the following links for more information about Clair:

* Clair GitHub repository (latest 2.x version as supported by this plugin): https://github.com/quay/clair/tree/v2.1.2
* Legacy Clair documentation: https://coreos.com/clair/docs/latest/ 

TODO: Provide further information and instructions

TODO: Provide comparison between the various Clair-related parser plugins (fortify-ssc-parser-clair-rest, fortify-ssc-parser-clair-yair, ...)

### Generate Clair JSON file

The following steps were used to generate the `src/test/resources/node_10.14.2-jessie.clair.rest.json` file:

```bash

# See instructions for fortify-ssc-parser-clair-yair to:
# - Set up Clair
# - Run a scan for the node:10.14.2-jessie image using Yair

# Once a scan has been run for the node:10.14.2-jessie image (using either 
# Yair or any other means), follow the instructions below to retrieve the
# JSON contents that can be parsed by fortify-ssc-parser-clair-rest


# Find the bottom layer id of the image that was scanned by Yair
# - 'docker manifest inspect' requires Docker experimental mode to be enabled
# - Requires jq to be installed
# - Other images may require slightly different approach (depending on manifest version)
# - Any better way of doing this?
layerId=`docker manifest inspect -v node:10.14.2-jessie | jq -r '.[0]["SchemaV2Manifest"]["layers"][-1]["digest"]'`
echo $layerId

# Get the features and vulnerabilities for this layer id (including all parent layers)
curl -X GET "http://localhost:6060/v1/layers/$layerId?features&vulnerabilities" -o  node_10.14.2-jessie.clair.rest.json
```

## IDE's

This project uses Lombok. In order to have your IDE compile this project without errors, 
you may need to add Lombok support to your IDE. Please see https://projectlombok.org/setup/overview 
for more information.


# Licensing

See [LICENSE.TXT](LICENSE.TXT)

