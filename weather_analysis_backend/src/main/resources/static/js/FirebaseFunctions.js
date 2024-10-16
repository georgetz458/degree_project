import { initializeApp } from "https://www.gstatic.com/firebasejs/10.0.0/firebase-app.js";
import { getAuth, signInWithEmailAndPassword, signOut, onAuthStateChanged  } from 'https://www.gstatic.com/firebasejs/10.0.0/firebase-auth.js'



// Για web app's Firebase configuration
const firebaseConfig = {
    apiKey: "AIzaSyDSFmxm04Hsrai1bEIosgBFBgr5cFzTdJQ",
    authDomain: "weatheranalysis-71e76.firebaseapp.com",
    projectId: "weatheranalysis-71e76",
    storageBucket: "weatheranalysis-71e76.appspot.com",
    messagingSenderId: "686816475514",
    appId: "1:686816475514:web:3ec5a83f5a02f0daae7ec3"
};
// Αρχικοποίηση Firebase
const app = initializeApp(firebaseConfig);
export const auth = getAuth();
export {signInWithEmailAndPassword, signOut};


export function logout(){
    signOut(auth).then(() => {

    }).catch((error) => {

        alert("error in logout");

    });
}

let currentUser;



onAuthStateChanged(auth, (user) => {
    showGuestUI();

    if (user) {
        currentUser = user;
        user.getIdToken(true).then((idToken) =>{
            showUI(user);
        }).catch((error) =>{
            logout();
            alert(error.message);
        });

    } else {

        const body = document.getElementById("page_content");
        if(body){
            body.classList.remove('hidden-body');
        }

        showGuestUI();

    }
});
const adminElements = document.getElementsByClassName("forAdmin");
const guestElements = document.getElementsByClassName("forGuest");
function showUI(user){
    user.getIdTokenResult()
        .then((idTokenResult) => {
            // Επιβεβαίωση ότι ο user είναι Admin.
            if(idTokenResult.claims.scope){

                const role = idTokenResult.claims.scope;

                const logOutButton = document.getElementById("logOutButton");
                if(logOutButton){
                    logOutButton.addEventListener('click', logout);
                }

                if(role !== 'ADMIN'){
                    const errorMessage = "You are not an admin! Login in this site is available only for admins!"; // Αρχικοποίηση του error message
                    sessionStorage.setItem("loginErrorMessage", errorMessage); // Αποθήκευση του error message στο session storage
                    location.replace("/loginPage"); // Ανακατεύθυνση στη σελίδα σύνδεσης
                    logout();

                }else {
                    showAdminUI();
                    const body = document.getElementById("page_content");
                    if(body){
                        body.classList.remove('hidden-body');
                    }

                }
            }else {
                logout();
            }

        })
        .catch((error) => {
            logout();
        });
}
function showAdminUI(){


    for(let i=0; i<guestElements.length;i++){
        guestElements[i].style.display = 'none';
    }
    for(let i=0; i<adminElements.length;i++){
        adminElements[i].style.display = 'block';
    }

}

function showGuestUI(){

    for(let i=0; i<adminElements.length;i++){
        adminElements[i].style.display = 'none';
    }
    for(let i=0; i<guestElements.length;i++){
        guestElements[i].style.display = 'block';
    }

}

