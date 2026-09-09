#!/bin/bash
echo "Setting up Android development environment..."
yes | sdkmanager --licenses
sdkmanager "platforms;android-34" \
  "build-tools;34.0.0" \
  "platform-tools" \
  "extras;google;m2repository" \
  "extras;android;m2repository"
echo 'export ANDROID_HOME=$HOME/android-sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/platform-tools' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools' >> ~/.bashrc
chmod +x gradlew
echo "Setup complete! Run './gradlew assembleDebug' to build."
