#!/bin/bash

echo ""
echo "Applying migration ViewArrivalNotifications"

echo "Adding routes to conf/app.routes"
echo "" >> ../conf/app.routes
echo "GET        /:mrn/viewArrivalNotifications                       controllers.ViewArrivalNotificationsController.onPageLoad(mrn: MovementReferenceNumber)" >> ../conf/app.routes

echo "Adding messages to conf.messages"
echo "" >> ../conf/messages.en
echo "viewArrivalNotifications.title = viewArrivalNotifications" >> ../conf/messages.en
echo "viewArrivalNotifications.heading = viewArrivalNotifications" >> ../conf/messages.en

echo "Migration ViewArrivalNotifications completed"
