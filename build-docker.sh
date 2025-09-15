#!/bin/bash

echo "🚀 Building Android APK with Docker..."
echo "This will take a few minutes on first run (downloading Android SDK)"
echo ""

# Build the Docker image
echo "📦 Building Docker image..."
docker build -t blackground-builder .

if [ $? -eq 0 ]; then
    echo ""
    echo "✅ Build completed successfully!"
    echo ""
    echo "📱 Extracting APK from Docker container..."
    
    # Create a temporary container and copy the APK
    CONTAINER_ID=$(docker create blackground-builder)
    docker cp $CONTAINER_ID:/app/app/build/outputs/apk/debug/app-debug.apk ./app-debug.apk
    docker rm $CONTAINER_ID
    
    if [ -f "./app-debug.apk" ]; then
        echo "✅ APK extracted successfully: ./app-debug.apk"
        echo ""
        echo "📲 To install on your Android device:"
        echo "   adb install app-debug.apk"
        echo ""
        ls -lh app-debug.apk
    else
        echo "❌ Failed to extract APK"
    fi
else
    echo "❌ Build failed!"
    exit 1
fi