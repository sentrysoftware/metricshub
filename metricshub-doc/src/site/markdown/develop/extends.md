keywords: develop, connector, extends
description: This page defines how to specify the connectors to extend.

# Extends

To defines a set of connectors to extends, you need to declare the connectors under the `extends` sections.

## Format

```yaml
connector:
  # ...
extends: <string-array>
```

In the extends array, you need to declare connectors using their file name without the .yaml extension.
Either a relative path can be used to point to the extended connector, or an absolute path, using the `connectors` directory as root.
