const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp(functions.config().Firebase);
var db = admin.firestore();
exports.newMemberNotification = functions.firestore
    .document('Teams/{teamId}/Waitlist/{userId}').onDelete((snap, context) => {
      // get the user we want to send the message to
      const newValue = snap.data();
      const teamidno = context.params.teamId;
      const useridno = newValue.userID;

      //start retrieving Waitlist user's messaging token to send them a message
      var tokenRef = db.collection('Users').doc(useridno);
      return tokenRef.get()
      .then(doc => {
        if (!doc.exists) {
          console.log('No such document!');
        } else {
          const data = doc.data();
          //get the messaging token
          var token = data.messaging_token;
          console.log("token: ", token);
          //reference for the members collection
          var memberRef = db.collection('Teams/'+teamidno+'/Members').doc(useridno);
          return memberRef.get()
          .then(doc => {
            if (!doc.exists){
              console.log('user was not added to team. Informing them');
              const negPayload = {
                data: {
                  data_type:"team_rejection",
                  title:"Request denied",
                  message: "Your request to join the team has been denied",
                }
              };
              return admin.messaging().sendToDevice(token, negPayload)
              .then(function(response){
                console.log("Successfully sent rejection message:", response);
                return Promise(response);
              })
              .catch(function(error){
                console.log("Error sending rejection message: ", error);
              });
            } else {
              console.log('user was added to the team. Informing them')
              const payload = {
                data: {
                  data_type: "team_accept",
                  title: "Request approved",
                  message: "You have been added to the team",
                }
              };
              return admin.messaging().sendToDevice(token, payload)
              .then(function(response){
                console.log("Successfully sent accept message:", response);
                return;
              })
              .catch(function(error){
                console.log("Error sending accept message: ", error);
              });
            }
          })
          .catch(err => {
            console.log('Error getting member', err);
          });
        }
        return Promise(doc.getData());
        })
        .catch(err => {
          console.log('Error getting token', err);
        });
    });
exports.removedFromTeam = functions.firestore.document('Teams/{teamId}/Members/{userId}').onDelete((snap, context) => {
    // get the user we want to send the message to
    const newValue = snap.data();
    const teamidno = context.params.teamId;
    const useridno = newValue.userID;

    //start retrieving Waitlist user's messaging token to send them a message
    var tokenRef = db.collection('Users').doc(useridno);
    return tokenRef.get()
    .then(doc => {
      if (!doc.exists) {
        console.log('No such document!');
      } else {
        const data = doc.data();
        //get the messaging token
        var token = data.messaging_token;
        console.log("token: ", token);
        const payload = {
          data: {
            data_type: "team_remove",
            title: "Membership Revoked",
            message: "You have been removed from the team",
          }
        };
        return admin.messaging().sendToDevice(token, payload)
        .then(function(response){
          console.log("Successfully sent accept message:", response);
          return;
        })
        .catch(function(error){
          console.log("Error sending accept message: ", error);
        });
      }
      return Promise(doc.getData());
    })
    .catch(err => {
        console.log('Error getting token', err);
      });

  });

  exports.answeredQuestion = functions.firestore.document('Teams/{teamId}/Questions/{questionID}').onUpdate((change, context) => {
      // Get an object representing the document
      const newValue = change.after.data();
      // access the needed information: the asker's id, the answers name
      const answerer = newValue.answerer;
      const askerID = newValue.askerID;

      //retrieve their token
      var tokenRef = db.collection('Users').doc(askerID);
      return tokenRef.get()
      .then(doc => {
        if (!doc.exists) {
          console.log('No such document!');
        } else {
          const data = doc.data();
          //get the messaging token
          var token = data.messaging_token;
          console.log("token: ", token);
          const payload = {
            data: {
              data_type: "question_answered",
              title: "Question answered",
              message: "Your question has been answered by "+answerer,
            }
          };
          return admin.messaging().sendToDevice(token, payload)
          .then(function(response){
            console.log("Successfully sent answered message:", response);
            return;
          })
          .catch(function(error){
            console.log("Error sending answered message: ", error);
          });
        }
        return Promise(doc.getData());
      })
      .catch(err => {
          console.log('Error getting token', err);
        });
    });
