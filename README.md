# cordova-plugin-onfido

This plugin can be used to interact with Onfido native SDK`s through cordova apps.

## Installation

Get stable version from npm:
```bash
 cordova plugin add cordova-plugin-onfido
```

Get latest master:
```bash
cordova plugin add https://github.com/rewireltd1/cordova-plugin-onfido
```

## Basic Usage

```typescript
const options = {
     token: 'mobile sdk token here', // IMPORTANT: see notes
     applicant_id: 'applicant id here',
     flow_steps: [ 'welcome', 'document', 'face', 'final'],
}

const onComplete = (completeResponse) => {
    alert(completeResponse)
}

window.cordova.plugins.onfido.init(onComplete, options);
```

# API Reference <a name="reference"></a>

* [onfido](#module_camera)
    * [.init(successCallback, options)](#module_camera.getPicture)  

---

<a name="module_camera"></a>

## onfido
<a name="module_camera.getPicture"></a>

### onfido.init(successCallback, options)
Takes options and inits Onfido native SDK

`options` example:
```typescript
    {
          token: [mobile sdk token], // SEE NOTES
          applicant_id: [ users applicant id ],
          flow_steps: [
            FlowTypes.Document,
            FlowTypes.Face,
          ],
          // documentTypes: [] // (currently not implmented in Onfido mobile sdks)
    }
```
`FlowTypes` options (typescript):
```typescript
enum FlowTypes {
  Welcome = 'welcome',
  Document = 'document',
  Face = 'face',
  FaceVideo = 'face_video',
  Final = 'final',
}
```

~~DocumentTypes~~ options (typescript): (currently not implmented in Onfido mobile sdks) :
```typescript
enum DocumentTypes {
  Passport = 'passport',
  nationalIdentityCard = 'national_identity_card',
  drivingLicence = 'driving_licence',
}
```

`successCallback` response:
```typescript
{
    document: {
        front: {
            id: 'doc id',
            type: 'doc type',
            side: 'doc side'
        },
        back: {
            id: 'doc id',
            type: 'doc type',
            side: 'doc side'
        }
    }
}
```
### NOTES
- __IMOPRTANT__: Please note that we are passing the mobile sdk token from javascript but it shouldnt be saved in the javascript bundle!
in the javascript code we are making authenticated request to our backend to retrieve it. see TODO`s for other methods

### TODO`s
- Passing mobile token from env variable:
    -  Create Cordova hooks that injects Onfido token to config.xml <preference name="onfidoToken" value="INJECT HERE" />
    -  Read token from preferences (how to do that: https://taivo.github.io/guides/read-config-xml-in-cordova)  

### Credits
- https://github.com/ihor-zhvanko, we used his code from https://github.com/ihor-zhvanko/cordova-plugin-onfido as a starting point 

## License

Copyright 2022 Rewire (O.S.G) Research and Development Ltd. All rights reserved.
Licensed under the Apache License, Version 2.0 (the "License"), see [LICENSE](https://github.com/rewireltd1/cordova-plugin-onfido/blob/master/LICENSE).

## How to Contribute

Contributors are welcome! And we need your contributions to keep the project moving forward. You can[report bugs, improve the documentation, or [contribute code](https://github.com/rewireltd1/cordova-plugin-onfido/pulls).

**Have a solution?** Send a [Pull Request](https://github.com/rewireltd1/cordova-plugin-onfido/pulls).
