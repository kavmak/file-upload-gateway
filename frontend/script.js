document.addEventListener("DOMContentLoaded", () => {
    // DOM elements
    const categorySelect = document.getElementById("categorySelect");
    const filetypeSelect = document.getElementById("filetypeSelect");
    const selectionError = document.getElementById("selectionError");
    
    const templateSection = document.getElementById("templateSection");
    const templateNameEl = document.getElementById("templateName");
    const headerPreview = document.getElementById("headerPreview");
    const downloadBtn = document.getElementById("downloadBtn");
    const downloadStatus = document.getElementById("downloadStatus");
    
    const fileInput = document.getElementById("fileInput");
    const applicationInput = document.getElementById("applicationInput");
    const uploadBtn = document.getElementById("uploadBtn");
    const uploadStatus = document.getElementById("uploadStatus");

    let currentTemplate = null;

    // =============================
    // 1. Load categories for dropdown
    // =============================
    function loadCategories() {
        const loadingOption = document.createElement("option");
        loadingOption.textContent = "Loading categories...";
        loadingOption.disabled = true;
        loadingOption.selected = true;
        categorySelect.appendChild(loadingOption);

        fetch("http://localhost:8082/api/gateway/templates/categories")
            .then((response) => {
                if (!response.ok) throw new Error("Network response was not ok");
                return response.json();
            })
            .then((categories) => {
                categorySelect.innerHTML = '<option value="">Select Category</option>';
                
                categories.forEach((category) => {
                    const option = document.createElement("option");
                    option.value = category;
                    option.textContent = category;
                    categorySelect.appendChild(option);
                });
            })
            .catch((err) => {
                console.error("Error fetching categories:", err);
                selectionError.textContent = "Could not load categories. Please refresh.";
                categorySelect.innerHTML = '<option value="" disabled>No categories available</option>';
            });
    }

    // =============================
    // 2. Handle template selection
    // =============================
    function handleTemplateSelection() {
        const category = categorySelect.value;
        const filetype = filetypeSelect.value;
        
        if (!category || !filetype) {
            templateSection.style.display = "none";
            currentTemplate = null;
            return;
        }

        selectionError.textContent = "";
        templateSection.style.display = "block";
        
        // Load template metadata
        loadTemplateMetadata(category, filetype);
    }

    function loadTemplateMetadata(category, filetype) {
        templateNameEl.textContent = "Loading...";
        headerPreview.innerHTML = '<span class="placeholder-text">Loading headers...</span>';
        
        const url = `http://localhost:8081/templates/${encodeURIComponent(category)}/metadata`;
        
        fetch(url)
            .then((response) => {
                if (!response.ok) throw new Error("Failed to fetch metadata");
                return response.json();
            })
            .then((meta) => {
                currentTemplate = { category, filetype, metadata: meta };
                
                templateNameEl.textContent = `${category} Template (${filetype.toUpperCase()})`;
                
                headerPreview.innerHTML = "";
                if (Array.isArray(meta.headers) && meta.headers.length > 0) {
                    meta.headers.forEach((header) => {
                        const pill = document.createElement("div");
                        pill.className = "header-pill";
                        pill.textContent = String(header);
                        headerPreview.appendChild(pill);
                    });
                } else {
                    headerPreview.innerHTML = '<span class="placeholder-text">No headers available</span>';
                }
            })
            .catch((err) => {
                console.error("Error fetching template metadata:", err);
                selectionError.textContent = "Could not load template metadata.";
                templateSection.style.display = "none";
                currentTemplate = null;
            });
    }

    // =============================
    // 3. Template download
    // =============================
    downloadBtn.addEventListener("click", () => {
        if (!currentTemplate) return;
        
        downloadStatus.textContent = "";
        downloadStatus.className = "status-message";
        downloadBtn.disabled = true;
        downloadBtn.textContent = "Downloading...";
        
        const url = `http://localhost:8081/templates/${encodeURIComponent(currentTemplate.category)}/download?format=${currentTemplate.filetype}`;
        
        fetch(url)
            .then((response) => {
                if (!response.ok) throw new Error("Download failed");
                return response.blob();
            })
            .then((blob) => {
                const url = window.URL.createObjectURL(blob);
                const a = document.createElement("a");
                a.href = url;
                a.download = `${currentTemplate.category}-template.${currentTemplate.filetype}`;
                document.body.appendChild(a);
                a.click();
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
                
                downloadStatus.textContent = "Template downloaded successfully!";
                downloadStatus.className = "status-message success";
            })
            .catch((err) => {
                console.error("Download error:", err);
                downloadStatus.textContent = "Download failed. Please try again.";
                downloadStatus.className = "status-message error";
            })
            .finally(() => {
                downloadBtn.disabled = false;
                downloadBtn.textContent = "Download Template";
            });
    });

    // =============================
    // 4. File upload
    // =============================
    uploadBtn.addEventListener("click", () => {
        uploadStatus.textContent = "";
        uploadStatus.className = "status-message";
        
        const file = fileInput.files[0];
        const application = applicationInput.value.trim();
        const category = categorySelect.value;
        
        if (!file) {
            uploadStatus.textContent = "Please select a file to upload.";
            uploadStatus.className = "status-message error";
            return;
        }
        
        if (!application) {
            uploadStatus.textContent = "Please enter an application name.";
            uploadStatus.className = "status-message error";
            return;
        }
        
        if (!category) {
            uploadStatus.textContent = "Please select a category first.";
            uploadStatus.className = "status-message error";
            return;
        }
        
        const formData = new FormData();
        formData.append("file", file);
        formData.append("application", application);
        formData.append("category", category);
        
        uploadBtn.disabled = true;
        uploadBtn.textContent = "Uploading...";
        
        fetch("http://localhost:8082/api/gateway/upload", {
            method: "POST",
            body: formData
        })
            .then(async (response) => {
                const result = await response.json().catch(() => ({ message: "Upload completed" }));
                
                if (!response.ok) {
                    throw new Error(result.message || "Upload failed");
                }
                
                uploadStatus.textContent = result.message || "File uploaded and validated successfully!";
                uploadStatus.className = "status-message success";
                
                // Display the actual JSON response data
                console.log("=== UPLOAD RESPONSE ===");
                console.log("Full Response:", result);
                if (result.data) {
                    console.log("Extracted JSON Data:", result.data);
                    console.log("JSON as String:", JSON.stringify(result.data, null, 2));
                }
                
                // Clear form
                fileInput.value = "";
                applicationInput.value = "";
            })
            .catch((err) => {
                console.error("Upload error:", err);
                uploadStatus.textContent = err.message || "Upload failed. Please try again.";
                uploadStatus.className = "status-message error";
            })
            .finally(() => {
                uploadBtn.disabled = false;
                uploadBtn.textContent = "Upload File";
            });
    });

    // =============================
    // Event listeners
    // =============================
    categorySelect.addEventListener("change", handleTemplateSelection);
    filetypeSelect.addEventListener("change", handleTemplateSelection);
    
    // Initialize
    loadCategories();
});