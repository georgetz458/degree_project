import {auth, signInWithEmailAndPassword, logout} from '/js/FirebaseFunctions.js'



export var hostURL = "https://localhost:8443";

const loginButton = document.getElementById('login');

const emailInput = document.getElementById('email');
const passwordInput = document.getElementById('password');
const loginErrorDiv = document.getElementById("login-error");
emailInput.addEventListener('input', () => {
    loginErrorDiv.style.display = 'none';
});

passwordInput.addEventListener('input', () => {
    loginErrorDiv.style.display = 'none';
});

document.addEventListener("DOMContentLoaded", () => {
    const errorMessage = sessionStorage.getItem("loginErrorMessage"); // Ανάκτηση του error message από session storage

    if (errorMessage) {
        const errorDiv = document.getElementById("login-error");
        errorDiv.textContent = errorMessage;
        errorDiv.style.display = "block";
        sessionStorage.removeItem("loginErrorMessage"); // Αφαίρεση του error message από session storage
    }
});


loginButton.addEventListener('click', login);



function validateIdToken(idToken) {

    const endpoint = "https://localhost:8443/login";
    const data = { "token": idToken };

    fetch(endpoint, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(data),
    })
        .then((response) => {
            if (response.ok) {

                location.replace(hostURL+"/");
            } else {

                disabledLoginButton = false;
                loginButton.classList.remove("disabled");
                displayError("Error in getting your role!\nPlease Try again later.")
            }
        })
        .catch((error) => {
            disabledLoginButton = false;
            loginButton.classList.remove("disabled");
            displayError(error.message)

        });
}
let disabledLoginButton = false;

function login() {
    if (!disabledLoginButton){
        disabledLoginButton = true;
        loginButton.classList.add("disabled");

        document.getElementById("login-error").style.display = 'none';

        let email = emailInput.value;
        let password = passwordInput.value;


        signInWithEmailAndPassword(auth, email, password)
            .then((userCredential) => {

                // Signed in
                const user = userCredential.user;
                if(user ){
                    userCredential.user.getIdToken().then((idToken) =>{


                        validateIdToken(idToken);
                    }).catch((error) =>{
                        disabledLoginButton = false;
                        loginButton.classList.remove("disabled");
                        displayError(error.code);
                    });


                }else{

                    logout();

                }
            })
            .catch((error) => {

                const errorCode = error.code;
                const errorMessage = error.message;
                disabledLoginButton = false;
                loginButton.classList.remove("disabled");
                displayError(errorCode);
            });
    }




}
function displayError(errorMessage){
    const loginError = errorMessage.replace('auth/', '').replace(/-/g, ' ');
    loginErrorDiv.style.display = 'block';
    loginErrorDiv.innerText = loginError.charAt(0).toUpperCase() + loginError.slice(1)+"!";
}


const cancelButton = document.getElementById("cancel");
cancelButton.addEventListener('click', ()=>{
    const previousPage = sessionStorage.getItem("previousPage"); // Retrieve the error message from session storage

    if (previousPage) {


        sessionStorage.removeItem("loginErrorMessage"); // Remove the error message from session storage
        location.replace(previousPage);
    }else{
        location.replace(hostURL+"/");
    }

});
