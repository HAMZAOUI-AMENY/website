// Function to handle logout
function logout() {
    fetch('/logout', {
        method: 'GET',
        credentials: 'include' // Ensure cookies are included in the request
    }).then(response => {
        if (response.ok) {
            window.location.href = '/login.html'; // Redirect to login page
        } else {
            alert('Logout failed');
        }
    }).catch(error => {
        console.error('Error during logout:', error);
        alert('Logout failed');
    });
}

// Add event listener to the logout button
document.getElementById('logoutButton').addEventListener('click', logout);
