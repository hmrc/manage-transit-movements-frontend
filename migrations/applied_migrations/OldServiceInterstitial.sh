#!/bin/bash

echo ""
echo "Applying migration OldServiceInterstitial"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /oldServiceInterstitial                       controllers.OldServiceInterstitialController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "oldServiceInterstitial.title = oldServiceInterstitial" >> ../conf/messages.en
echo "oldServiceInterstitial.heading = oldServiceInterstitial" >> ../conf/messages.en

echo "Migration OldServiceInterstitial completed"
