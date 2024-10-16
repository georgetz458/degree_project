export var hostURL = "https://localhost:8443";



const goLoginButton = document.getElementById("loginButton");
if(goLoginButton){
    goLoginButton.addEventListener('click', ()=>{
        goLogin(location.href);
    });
}

function goLogin(currentUrl){
    sessionStorage.setItem("previousPage", currentUrl);
    location.replace(hostURL+"/loginPage");
}
