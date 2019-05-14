# testapp

- Used KotlinCoroutines instead of RxJava
- CameraKit api required workarounds:
  - Had to be the opening fragment >> captureImage callback isn't called.
  - Can't initialize in background/locked screen >> Shows black screen
  - Response isn't quick >> callback won't be called if button presed without letting the camera focus.
  
  - Restart app in case of above errors.
- UI/UX can be better.
