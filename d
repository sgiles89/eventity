[1mdiff --cc app/src/main/java/com/example/stepheng/eventity/JoinTeamActivity.java[m
[1mindex fe2666b,7916e22..0000000[m
[1m--- a/app/src/main/java/com/example/stepheng/eventity/JoinTeamActivity.java[m
[1m+++ b/app/src/main/java/com/example/stepheng/eventity/JoinTeamActivity.java[m
[36m@@@ -112,11 -118,15 +116,21 @@@[m [mpublic class JoinTeamActivity extends A[m
                              //add user as the owner[m
                              joinWaitlist.set(waitlistData);[m
  [m
[32m++<<<<<<< HEAD[m
[32m +                            //updating user profile with pending membership for new team[m
[32m +                            DocumentReference updateMemberships = mFStore.collection("Users/"+user_id+"/Membership").document("Membership");[m
[32m +                            Map<String, Object> membershipData = new HashMap<>();[m
[32m +                            membershipData.put("teamID", teamId);[m
[32m +                            membershipData.put("role", "pending");[m
[32m+                             DocumentReference pendingMember = mFStore.collection("Users/"+user_id+"/Membership").document(user_id);[m
[32m+                             Map<String, Object> membershipData = new HashMap<>();[m
[32m+                             membershipData.put("TeamID", teamId);[m
[32m+                             membershipData.put("Membership", "pending");[m
[32m+                             pendingMember.set(membershipData);[m
[32m+ [m
[32m+                             //show success message and send to Main Activity[m
[32m+                             Toast.makeText(JoinTeamActivity.this, "Team Request sent", Toast.LENGTH_LONG).show();[m
[32m+                             sendtoMain();[m
  [m
                          } else {[m
                              Log.d(TAG, "Error getting documents: ", task.getException());[m
[1mdiff --git a/.idea/modules.xml b/.idea/modules.xml[m
[1mindex 2609a22..02d9d55 100644[m
[1m--- a/.idea/modules.xml[m
[1m+++ b/.idea/modules.xml[m
[36m@@ -2,8 +2,8 @@[m
 <project version="4">[m
   <component name="ProjectModuleManager">[m
     <modules>[m
[31m-      <module fileurl="file://$PROJECT_DIR$/Eventity.iml" filepath="$PROJECT_DIR$/Eventity.iml" />[m
       <module fileurl="file://$PROJECT_DIR$/app/app.iml" filepath="$PROJECT_DIR$/app/app.iml" />[m
[32m+[m[32m      <module fileurl="file://$PROJECT_DIR$/eventity.iml" filepath="$PROJECT_DIR$/eventity.iml" />[m
     </modules>[m
   </component>[m
 </project>[m
\ No newline at end of file[m
