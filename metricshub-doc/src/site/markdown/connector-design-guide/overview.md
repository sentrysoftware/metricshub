keywords: guide, overview, connector
description: The intended audience of this guide is the advanced user of MetricsHub, who feels confident enough to dig into the depth of connector development.

# Overview

The goal of this guide is to explain how to develop your own connectors.

We will see how is constructed a connector, what is the syntax of each its parts and what they mean.

The targeted audience of this guide is the advanced user of MetricsHub who wishes to monitor some specific appliance that is not covered by MetricsHub's built in connectors.

## General Syntax

The general syntax for *.yaml files is as in the example below.

```yaml
<property>: <value>
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