document.addEventListener('DOMContentLoaded', () => {
    fetchOptions();
});

async function fetchOptions() {
    try {
        const response = await fetch('http://localhost/civic_backend/options.php');
        const data = await response.json();
        
        renderOptions(data.organization, 'org-options-grid');
        renderOptions(data.user, 'user-options-grid');
    } catch (error) {
        console.error('Error fetching options:', error);
        document.getElementById('org-options-grid').innerHTML = '<p class="error-msg">Failed to load organization options.</p>';
        document.getElementById('user-options-grid').innerHTML = '<p class="error-msg">Failed to load user options.</p>';
    }
}

function renderOptions(options, containerId) {
    const container = document.getElementById(containerId);
    container.innerHTML = ''; // Clear loading state or previous data
    
    options.forEach(option => {
        const card = document.createElement('div');
        card.className = 'option-card';
        card.innerHTML = `
            <div class="option-icon-wrapper">
                <span class="material-symbols-rounded option-icon">${option.icon}</span>
            </div>
            <div class="option-content">
                <h3 class="option-title">${option.title}</h3>
                <p class="option-desc">${option.description}</p>
            </div>
            <div class="option-arrow">
                <span class="material-symbols-rounded">chevron_right</span>
            </div>
        `;
        
        // Add subtle interaction
        card.addEventListener('click', () => {
            console.log(`Clicked on ${option.id}`);
            // Here you would navigate to the specific setting page
            card.style.transform = 'scale(0.98)';
            setTimeout(() => {
                card.style.transform = '';
            }, 150);
        });
        
        container.appendChild(card);
    });
}

