document.addEventListener("DOMContentLoaded", () => {
    const registrationForm = document.getElementById("registrationForm");
    const registrationStatus = document.getElementById("registrationStatus");
    const successInfo = document.getElementById("successInfo");
    const appHashDisplay = document.getElementById("appHashDisplay");
    const integrationUrl = document.getElementById("integrationUrl");
    const copyHashBtn = document.getElementById("copyHashBtn");
    const copyUrlBtn = document.getElementById("copyUrlBtn");
    const submitBtn = registrationForm.querySelector('button[type="submit"]');
    const templateFileInput = document.getElementById("templateFile");
    
    // Check form completion and toggle button state
    function checkFormCompletion() {
        const appName = document.getElementById("appName").value.trim();
        const category = document.getElementById("category").value.trim();
        const endpoint = document.getElementById("endpoint").value.trim();
        const templateFile = templateFileInput.files[0];
        
        if (appName && category && endpoint && templateFile) {
            submitBtn.classList.remove("btn-faded");
            submitBtn.classList.add("btn-active");
        } else {
            submitBtn.classList.remove("btn-active");
            submitBtn.classList.add("btn-faded");
        }
    }
    
    // Add event listeners to all form inputs
    document.getElementById("appName").addEventListener("input", checkFormCompletion);
    document.getElementById("category").addEventListener("input", checkFormCompletion);
    document.getElementById("endpoint").addEventListener("input", checkFormCompletion);
    templateFileInput.addEventListener("change", checkFormCompletion);

    registrationForm.addEventListener("submit", async (e) => {
        e.preventDefault();
        
        const appName = document.getElementById("appName").value.trim();
        const category = document.getElementById("category").value.trim();
        const endpoint = document.getElementById("endpoint").value.trim();
        const templateFile = document.getElementById("templateFile").files[0];
        
        if (!appName || !category || !endpoint || !templateFile) {
            showStatus("Please fill all fields and select a template file.", "error");
            return;
        }
        
        const formData = new FormData();
        formData.append("appName", appName);
        formData.append("category", category);
        formData.append("endpoint", endpoint);
        formData.append("template", templateFile);
        
        const submitBtn = registrationForm.querySelector('button[type="submit"]');
        submitBtn.disabled = true;
        submitBtn.textContent = "Registering...";
        
        try {
            const response = await fetch("http://localhost:8081/api/register", {
                method: "POST",
                body: formData
            });
            
            const result = await response.json();
            
            if (result.success) {
                showStatus("Registration successful!", "success");
                showSuccessInfo(result.appNameHash, appName);
                registrationForm.reset();
                checkFormCompletion(); // Reset button state
            } else {
                showStatus(`Registration failed: ${result.message}`, "error");
            }
            
        } catch (error) {
            console.error("Registration error:", error);
            showStatus("Registration failed. Please check if services are running.", "error");
        } finally {
            submitBtn.disabled = false;
            submitBtn.textContent = "Register Template";
        }
    });
    
    function showStatus(message, type) {
        registrationStatus.textContent = message;
        registrationStatus.className = `status-message ${type}`;
        
        // Hide success info if showing error
        if (type === "error") {
            successInfo.style.display = "none";
        }
    }
    
    function showSuccessInfo(appHash, appName) {
        appHashDisplay.value = appHash;
        integrationUrl.value = `${window.location.origin}/index.html?app=${appHash}`;
        successInfo.style.display = "block";
    }
    
    // Copy functionality
    copyHashBtn.addEventListener("click", () => {
        appHashDisplay.select();
        document.execCommand("copy");
        copyHashBtn.textContent = "Copied!";
        setTimeout(() => {
            copyHashBtn.textContent = "Copy";
        }, 2000);
    });
    
    copyUrlBtn.addEventListener("click", () => {
        integrationUrl.select();
        document.execCommand("copy");
        copyUrlBtn.textContent = "Copied!";
        setTimeout(() => {
            copyUrlBtn.textContent = "Copy";
        }, 2000);
    });
});