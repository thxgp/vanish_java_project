/**
 * ==========================================================================
 * VANISH - One-Time Secret Note Handoff
 * Interactive Note Composition & JSON Serialization Handler (Phase 3 & 4)
 * ==========================================================================
 */

document.addEventListener('DOMContentLoaded', () => {
  const noteForm = document.getElementById('composeNoteForm');
  const secretTextarea = document.getElementById('secretText');
  const charCounter = document.getElementById('charCounter');
  const expirySelect = document.getElementById('expiryMinutes');
  const viewsSelect = document.getElementById('views');
  const submitBtn = document.getElementById('composeSubmitBtn');
  const jsonPreviewBtn = document.getElementById('viewJsonBtn');
  const jsonModal = document.getElementById('jsonModal');
  const jsonOutput = document.getElementById('jsonOutput');
  const closeModalBtn = document.getElementById('closeModalBtn');
  const copyModalJsonBtn = document.getElementById('copyModalJsonBtn');

  // Result Card Elements
  const resultCard = document.getElementById('composeResultCard');
  const generatedLinkInput = document.getElementById('generatedLink');
  const copyLinkBtn = document.getElementById('copyLinkBtn');
  const cipherPreview = document.getElementById('cipherPreview');

  // 1. Live Character Count
  if (secretTextarea && charCounter) {
    const MAX_CHARS = 500;
    const updateCounter = () => {
      const remaining = MAX_CHARS - secretTextarea.value.length;
      charCounter.textContent = `${secretTextarea.value.length} / ${MAX_CHARS} chars`;
      if (remaining < 50) {
        charCounter.style.color = 'var(--accent-amber)';
      } else {
        charCounter.style.color = 'var(--text-muted)';
      }
      if (remaining < 0) {
        charCounter.style.color = 'var(--danger)';
      }
    };
    secretTextarea.addEventListener('input', updateCounter);
    updateCounter();
  }

  // 2. JSON Payload Preview Modal (CLO2 requirement: represent user data using JSON)
  if (jsonPreviewBtn && jsonModal && jsonOutput) {
    jsonPreviewBtn.addEventListener('click', (e) => {
      e.preventDefault();
      const owner = document.body.dataset.currentUser || 'student';
      const text = secretTextarea ? secretTextarea.value : '';
      const exp = expirySelect ? expirySelect.value : 10;
      const views = viewsSelect ? viewsSelect.value : 1;

      const payload = VanishValidation.toNoteJson(owner, text, exp, views);
      jsonOutput.textContent = payload;
      jsonModal.classList.add('active');
    });

    if (closeModalBtn) {
      closeModalBtn.addEventListener('click', () => {
        jsonModal.classList.remove('active');
      });
    }

    if (copyModalJsonBtn) {
      copyModalJsonBtn.addEventListener('click', () => {
        navigator.clipboard.writeText(jsonOutput.textContent).then(() => {
          showToast('JSON payload copied to clipboard!', 'success');
        });
      });
    }

    jsonModal.addEventListener('click', (e) => {
      if (e.target === jsonModal) {
        jsonModal.classList.remove('active');
      }
    });
  }

  // 3. Form Submission Handling with Validation Flow
  if (noteForm) {
    noteForm.addEventListener('submit', async (e) => {
      // If server form submission is preferred without AJAX, we validate first
      const textVal = secretTextarea ? secretTextarea.value : '';
      const expVal = expirySelect ? expirySelect.value : 10;
      const viewsVal = viewsSelect ? viewsSelect.value : 1;

      const textCheck = VanishValidation.validateNoteText(textVal);
      const expCheck = VanishValidation.validateExpiry(expVal);
      const viewsCheck = VanishValidation.validateViews(viewsVal);

      const textError = document.getElementById('secretTextError');
      VanishValidation.setFieldState(secretTextarea, textError, textCheck);

      if (!textCheck.valid || !expCheck.valid || !viewsCheck.valid) {
        e.preventDefault();
        showToast('Please fix validation errors before creating note.', 'error');
        if (secretTextarea) secretTextarea.focus();
        return;
      }

      // Check if running in Phase 3 standalone client-side demo mode
      if (noteForm.dataset.demoMode === 'true') {
        e.preventDefault();
        submitBtn.disabled = true;
        submitBtn.innerHTML = '<span>Encrypting & Creating...</span>';

        setTimeout(() => {
          submitBtn.disabled = false;
          submitBtn.innerHTML = '<span>Generate Secret Link</span>';

          // Simulate cryptographically secure token & AES preview
          const randomToken = Math.random().toString(36).substring(2, 10);
          const fullLink = `${window.location.origin}/vanish/read?t=${randomToken}`;

          if (resultCard && generatedLinkInput) {
            generatedLinkInput.value = fullLink;
            if (cipherPreview) {
              cipherPreview.textContent = btoa(textVal).substring(0, 24) + '... (AES-256 in memory)';
            }
            resultCard.style.display = 'block';
            resultCard.scrollIntoView({ behavior: 'smooth' });
          }
          showToast('Secret note encrypted and link generated!', 'success');
        }, 600);
      }
    });
  }

  // 4. One-Click Copy Link
  if (copyLinkBtn && generatedLinkInput) {
    copyLinkBtn.addEventListener('click', () => {
      generatedLinkInput.select();
      navigator.clipboard.writeText(generatedLinkInput.value).then(() => {
        showToast('Link copied to clipboard! Share it safely.', 'success');
      });
    });
  }
});

/**
 * Global Toast Notification Helper
 */
function showToast(message, type = 'info') {
  let container = document.querySelector('.toast-container');
  if (!container) {
    container = document.createElement('div');
    container.className = 'toast-container';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.className = `toast toast-${type}`;
  const icon = type === 'success' ? '✓' : (type === 'error' ? '⚠' : 'ℹ');
  toast.innerHTML = `<span style="font-weight:bold; font-size:1.1rem">${icon}</span> <span>${message}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.style.opacity = '0';
    toast.style.transform = 'translateX(20px)';
    toast.style.transition = 'all 0.3s ease';
    setTimeout(() => toast.remove(), 300);
  }, 4000);
}
window.showToast = showToast;
