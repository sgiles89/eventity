const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().Firebase);
var db = admin.firestore();
exports.newMemberNotification = functions.firestore
    .document('Teams/{teamId}/Members/{userId}').onCreate((snap, context) => {
      // get the user we want to send the message to
      const newValue = snap.data();
      const useridno = newValue.userID;
      var tokenRef = db.collection('Users').doc(useridno);
      tokenRef.get()
    .then(doc => {
      if (!doc.exists) {
        console.log('No such document!');
      } else {
        const data = doc.data();
        var token = data.messaging_token;
        console.log("token: ", token);
        const payload = {
          data: {
              data_type: "direct_message",
              title: "Request approved",
              message: "You have been added to the team",
          }
        }
        console.log("the payload was", payload);
        return admin.messaging().sendToDevice(token, payload)
          .then(function(response){
            console.log("Successfully sent message:", response);
            return db.collection('Users/'+useridno+"/Notifications").add({
              title: 'Request approved',
              message: 'Your request to join the team has been approved'
            }).then(ref => {
              console.log('Added document with ID: ', ref.id);
              return ref
            });
          })
          .catch(function(error){
            console.log("Error sending message: ", error);
          });
      }
      return 5;
    })
    .catch(err => {
      console.log('Error getting document', err);
    });
});
// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
// exports.helloWorld = functions.https.onRequest((request, response) => {
//  response.send("Hello from Firebase!");
// });
