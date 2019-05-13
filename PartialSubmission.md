# testapp

Things that I did:
  - Checking for Phone number in database
  - If exists, increment visit_count
  - If not, authenticate.
    - If authenticated, add new visitor
    - If not, add suspicious user.
  
  - Taking photo and compressing it without Rxjava.
  - Uploading to storage and getting the download url
  - Above two things are not tested together, as I couldn't find a workaround for bug#467
        - https://github.com/CameraKit/camerakit-android/issues/467

Things that are left:
  - Using Rxjava
  - Number of bugs
  - UI/UX
