#!/bin/bash

echo ""
echo "Applying migration XmlNegativeAcknowledgement"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /xmlNegativeAcknowledgement                       controllers.XmlNegativeAcknowledgementController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "xmlNegativeAcknowledgement.title = xmlNegativeAcknowledgement" >> ../conf/messages.en
echo "xmlNegativeAcknowledgement.heading = xmlNegativeAcknowledgement" >> ../conf/messages.en

echo "Migration XmlNegativeAcknowledgement completed"
