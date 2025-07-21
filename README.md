# **StreamX Connector Github**

StreamX Connector GitHub is a Quarkus GitHub Action project that allows syncing GitHub 
changes with StreamX.

Detail information please check [Quarkus GitHub Action](https://docs.quarkiverse.io/quarkus-github-action/dev/index.html) documentation.

## **Actions**
* WebResourceAction - This action checks your repository under $GITHUB_WORKSPACE, if there are any
git modifications in files, that are configured for monitoring in variable `STREAMX_INGESTION_WEBRESOURCE_INCLUDES`.
Aditionaly by using `Run workflow` button with `Publish all` option checked we trigger full
sync of the repository with StreamX.

### Usage WebResourceAction

#### Prerequisites

* job execution has if constraint that allows only pull request closed and manual workflow dispatch  
executions, ie:
```yaml
    if: github.event.pull_request.merged == true || (github.event_name == 'workflow_dispatch' && inputs.publish_all_webresources == true)
```
* working directory has already checkout code base, with defined fetch-depth option set to 0 (option required for git diff detection)
```yaml
    - name: Checkout code
      uses: actions/checkout@v4
      with:
        fetch-depth: 0
```

#### Usage
<!-- start usage -->
```yaml
- name: Run sync with StreamX
  uses: streamx-dev/streamx-connector-github@main
  with:
    # Name of the action with 'webresource_' prefix. Supported event names are [pull_request, workflow_dispatch]
    action: "webresource_${{github.event_name}}"
    
    # A secret parameter with auth token used for communication with StreamX ingestion API. 
    # Value required.
    streamx-ingestion-token: ${{ secret.STREAMX_INGESTION_TOKEN }}

     # A variable parameter with StreamX ingestion api endpoint URL. 
     # Example: https://ingestion.mystreamx.site
     # Value required.
    streamx-ingestion-url: ${{ vars.STREAMX_INGESTION_URL }}

    # A variable parameter list of paths that are included in StreamX webresources syncing processing.
    # Check mechanism is based on ant path matcher code base [details ref java.nio.file.PathMatcher]. 
    # Example: ["scripts/*.js", "styles/*.css"]
    # Value required.    
    streamx-ingestion-webresource-includes: ${{ vars.STREAMX_INGESTION_WEBRESOURCE_INCLUDES }}
```
<!-- end usage -->
#### Example workflow
```yaml
name: Publish/Unpublish web resources on StreamX

on:
  workflow_dispatch:
    inputs:
      publish_all_webresources:
        description: "Publish all pattern included webresources to StreamX"
        required: false
        type: boolean
        default: false
  pull_request:
    types:
      - closed
    branches:
      - main

jobs:
  sync-with-streamx:
    if: github.event.pull_request.merged == true || (github.event_name == 'workflow_dispatch' && inputs.publish_all_webresources == true)
    runs-on: ubuntu-latest
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
      - name: Run sync with StreamX
        uses: streamx-dev/streamx-connector-github@main
        with:
          action: "webresource_${{github.event_name}}"
          streamx-ingestion-token: ${{ secrets.STREAMX_INGESTION_TOKEN }}
          streamx-ingestion-url: ${{ vars.STREAMX_INGESTION_URL }}
          streamx-ingestion-webresource-includes: ${{ vars.STREAMX_INGESTION_WEBRESOURCE_INCLUDES }}
```

## **Releasing**

Please follow [Quarkus GitHub Action](https://docs.quarkiverse.io/quarkus-github-action/dev/push-to-production.html) documentation recommendation steps.