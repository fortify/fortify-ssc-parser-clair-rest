This Fortify SSC parser plugin allows for importing scan results from Clair (Vulnerability Static Analysis for Containers).

Clair itself doesn't provide any file-based reports; as such this parser plugin parses files containing JSON produced by the
Clair 2.x `/v1/layers/{layerId}?features&vulnerabilities` REST API call