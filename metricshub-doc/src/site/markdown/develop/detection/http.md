keywords: develop, connector, criteria
description: This page defines the detection’s criteria that are defined in a connector.

# HTTP (Detection)

<!-- MACRO{toc|fromDepth=1|toDepth=1|id=toc} -->

The goal of this part is to see how to define HTTP criteria.

```yaml
connector:
  # ...
  detection: # <object>
    # ...
    criteria: # <object-array>
    - type: http
      method: # <enum> | possible values: [ get, post, delete, put ]
      path: # <string>
      header: # <string>
      body: # <string>
      expectedResult: # <string>
      resultContent: # <enum> | possible values: [ httpStatus, header, body, all ]
      authenticationToken: # <string>
      errorMessage: # <string>
```

### Input Properties

| Input Property        | Description       |
| --------------------- | ----------------- |
| `method` | The HTTP request method type: `get`, `post`, `delete`, put (default: `get`) |
| `path` | The path to connect to.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `header` | The HTTP request’s header.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `body` | The HTTP request’s body.<br />Macros such as `%{AUTHENTICATIONTOKEN}`, `%{USERNAME}`, `%{PASSWORD}`, `%{PASSWORD_BASE64}`, `%{BASIC_AUTH_BASE64}` or `%{AUTHENTICATIONTOKEN}` may be used. |
| `expectedResult` | Regular expression that is expected to match the result of the `HTTP` request. |
| `resultContent` | Extracts the specified content from the HTTP request’s result (default: `body`). |
| `authenticationToken` | The authentication token (typically a reference to another source). |
| `errorMessage` | The error message to display if the expectedResult regular expression evaluates to false. |

### Example

```yaml
connector:
  detection:
    criteria:
    - type: http
      method: GET
      path: /api/DeviceService/Devices
      header: ${esc.d}{file::http-header}
      expectedResult: api
      errorMessage: Failed to get response from API
```
