#!/bin/bash

echo ""
echo "Applying migration ViewAllArrivals"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /viewAllArrivals                       controllers.ViewAllArrivalsController.onPageLoad" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "viewAllArrivals.title = viewAllArrivals" >> ../conf/messages.en
echo "viewAllArrivals.heading = viewAllArrivals" >> ../conf/messages.en

echo "Migration ViewAllArrivals completed"
