#!/bin/bash

echo ""
echo "Applying migration NorthernIrelandInterstitial"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /northernIrelandInterstitial                       controllers.NorthernIrelandInterstitialController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "northernIrelandInterstitial.title = northernIrelandInterstitial" >> ../conf/messages.en
echo "northernIrelandInterstitial.heading = northernIrelandInterstitial" >> ../conf/messages.en

echo "Migration NorthernIrelandInterstitial completed"
