keywords: guide, overview, connector
description: The intended audience of this guide is the advanced user of MetricsHub, who feels confident enough to dig into the depth of connector development.

# Overview

## General Syntax

The general syntax for *.yaml files is as in the example below.

```yaml
<property>=<value>
```

Comments are prefixed with an hash symbol ('#')

```yaml
# This is a comment
```

## Structure

All connectors follow the structure below:
* Connector general properties
* Detection criteria
* The monitor operations tree, starting with the key word "monitors:"
    * The Resource type (eg. disk_controller)
        * Discovery source and compute operations
        * Discovery mapping
        * Collect type (mono or multi Instances)
        * Collect source and compute operations
        * Collect mapping
 * Translations tables