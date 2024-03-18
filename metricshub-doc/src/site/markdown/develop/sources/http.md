keywords: http, rest, api
description: The HTTP source queries any resource available through HTTP or HTTPS, like a REST API endpoint or a Web service.

# HTTP (Source)

```yaml
connector:
  # ...
pre: # <object>
  <sourceKey>: # <source-object>

monitors:
  <monitorType>: # <object>
    <job>: # <object>
      sources: # <object>
        <sourceKey>:
          type: http
          method: # <enum> | possible values [ get, post, delete, put ]
          path: # <string>
          header: # <string>
          body: # <string>
          authenticationToken: # <string>
          resultContent: # <enum> | possible values: [ httpStatus, header, body, all ]
          forceSerialization: <boolean>
          executeForEachEntryOf: # <object>
            source: # <string>
            concatMethod: # onOf [ <enum>, <object> ] | possible values for <enum> : [ list, json_array, json_array_extended ]
            concatStart: # <string>
            concatEnd: # <string>
          computes: # <compute-object-array>
```
