/**
 * ==========================================================================
 * VANISH - One-Time Secret Note Handoff
 * Client-Side JavaScript Validation Engine (Phase 3 & Phase 4)
 * Covers: Empty field checks, Format checks, Live Password Strength,
 * JSON Data Representation & Form Interaction Flow.
 * ==========================================================================
 */

const VanishValidation = (() => {
  // Constants matching backend ValidationUtil specifications
  const RULES = {
    USERNAME: {
      MIN: 3,
      MAX: 20,
      REGEX: /^[a-zA-Z0-9_]+$/,
    },
    PASSWORD: {
      MIN: 8,
      HAS_UPPER: /[A-Z]/,
      HAS_LOWER: /[a-z]/,
      HAS_DIGIT: /[0-9]/,
      HAS_SPECIAL: /[!@#$%^&*()_+\-=[\]{};':"\\|,.<>/?]/,
    },
    NOTE: {
      MAX_LENGTH: 500,
      MIN_EXPIRY: 1,
      MAX_EXPIRY: 1440,
      MIN_VIEWS: 1,
      MAX_VIEWS: 5,
    }
  };

  /**
   * Validates a username string.
   * Returns { valid: boolean, message: string }
   */
  function validateUsername(username) {
    if (!username || username.trim().length === 0) {
      return { valid: false, message: 'Username cannot be empty.' };
    }
    const trimmed = username.trim();
    if (trimmed.length < RULES.USERNAME.MIN) {
      return { valid: false, message: `Username must be at least ${RULES.USERNAME.MIN} characters long.` };
    }
    if (trimmed.length > RULES.USERNAME.MAX) {
      return { valid: false, message: `Username cannot exceed ${RULES.USERNAME.MAX} characters.` };
    }
    if (username.indexOf(' ') !== -1) {
      return { valid: false, message: 'Username cannot contain spaces.' };
    }
    if (!RULES.USERNAME.REGEX.test(trimmed)) {
      return { valid: false, message: 'Username can only contain letters, numbers, and underscores.' };
    }
    return { valid: true, message: 'Username is valid.' };
  }

  /**
   * Validates a password string and calculates strength metrics.
   * Returns { valid: boolean, message: string, score: number, checks: object }
   */
  function validatePassword(password) {
    if (!password || password.trim().length === 0) {
      return {
        valid: false,
        message: 'Password cannot be empty.',
        score: 0,
        checks: { length: false, upper: false, lower: false, digit: false, special: false }
      };
    }

    const checks = {
      length: password.length >= RULES.PASSWORD.MIN,
      upper: RULES.PASSWORD.HAS_UPPER.test(password),
      lower: RULES.PASSWORD.HAS_LOWER.test(password),
      digit: RULES.PASSWORD.HAS_DIGIT.test(password),
      special: RULES.PASSWORD.HAS_SPECIAL.test(password),
    };

    let score = 0;
    if (checks.length) score += 20;
    if (checks.upper) score += 20;
    if (checks.lower) score += 20;
    if (checks.digit) score += 20;
    if (checks.special) score += 20;

    let message = 'Password meets all security criteria.';
    let valid = true;

    if (!checks.length) {
      message = `Password must be at least ${RULES.PASSWORD.MIN} characters.`;
      valid = false;
    } else if (!checks.upper) {
      message = 'Password must include at least one uppercase letter (A-Z).';
      valid = false;
    } else if (!checks.lower) {
      message = 'Password must include at least one lowercase letter (a-z).';
      valid = false;
    } else if (!checks.digit) {
      message = 'Password must include at least one number (0-9).';
      valid = false;
    } else if (!checks.special) {
      message = 'Password must include at least one special character (!@#$%^&*...).';
      valid = false;
    }

    return { valid, message, score, checks };
  }

  /**
   * Validates confirmation password match.
   */
  function validatePasswordConfirmation(password, confirmPassword) {
    if (!confirmPassword || confirmPassword.length === 0) {
      return { valid: false, message: 'Please confirm your password.' };
    }
    if (password !== confirmPassword) {
      return { valid: false, message: 'Passwords do not match.' };
    }
    return { valid: true, message: 'Passwords match.' };
  }

  /**
   * Validates secret note text content.
   */
  function validateNoteText(text) {
    if (!text || text.trim().length === 0) {
      return { valid: false, message: 'Note text cannot be empty or whitespace only.' };
    }
    const length = text.trim().length;
    if (length > RULES.NOTE.MAX_LENGTH) {
      return { valid: false, message: `Note cannot exceed ${RULES.NOTE.MAX_LENGTH} characters (currently ${length}).` };
    }
    return { valid: true, message: 'Note content is valid.' };
  }

  /**
   * Validates expiry minutes.
   */
  function validateExpiry(minutes) {
    const min = parseInt(minutes, 10);
    if (isNaN(min) || min < RULES.NOTE.MIN_EXPIRY || min > RULES.NOTE.MAX_EXPIRY) {
      return { valid: false, message: `Expiry must be between ${RULES.NOTE.MIN_EXPIRY} and ${RULES.NOTE.MAX_EXPIRY} minutes.` };
    }
    return { valid: true, message: 'Expiry is valid.' };
  }

  /**
   * Validates view count limit.
   */
  function validateViews(views) {
    const v = parseInt(views, 10);
    if (isNaN(v) || v < RULES.NOTE.MIN_VIEWS || v > RULES.NOTE.MAX_VIEWS) {
      return { valid: false, message: `Views must be between ${RULES.NOTE.MIN_VIEWS} and ${RULES.NOTE.MAX_VIEWS}.` };
    }
    return { valid: true, message: 'View count is valid.' };
  }

  /**
   * UI Helper: Sets input field validation state and error message display.
   */
  function setFieldState(inputEl, errorEl, result) {
    if (!inputEl) return;
    if (result.valid) {
      inputEl.classList.remove('is-invalid');
      inputEl.classList.add('is-valid');
      if (errorEl) {
        errorEl.classList.remove('show');
        errorEl.textContent = '';
      }
    } else {
      inputEl.classList.remove('is-valid');
      inputEl.classList.add('is-invalid');
      if (errorEl) {
        errorEl.classList.add('show');
        errorEl.innerHTML = `<svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="12" cy="12" r="10"></circle><line x1="12" y1="8" x2="12" y2="12"></line><line x1="12" y1="16" x2="12.01" y2="16"></line></svg> ${result.message}`;
      }
    }
  }

  /**
   * Represents user registration data in standardized JSON format.
   * Requirement: CLO2 "Represent user data format using JSON (simple structure)"
   */
  function toUserJson(username, password, confirmPassword) {
    const userData = {
      username: username ? username.trim() : '',
      password: password || '',
      confirmPassword: confirmPassword || '',
      timestamp: new Date().toISOString()
    };
    return JSON.stringify(userData, null, 2);
  }

  /**
   * Represents secret note creation payload in standardized JSON format.
   */
  function toNoteJson(owner, secretText, expiryMinutes, views) {
    const noteData = {
      owner: owner ? owner.trim() : 'anonymous',
      secretText: secretText ? secretText.trim() : '',
      expiryMinutes: parseInt(expiryMinutes, 10) || 10,
      views: parseInt(views, 10) || 1,
      timestamp: new Date().toISOString()
    };
    return JSON.stringify(noteData, null, 2);
  }

  return {
    RULES,
    validateUsername,
    validatePassword,
    validatePasswordConfirmation,
    validateNoteText,
    validateExpiry,
    validateViews,
    setFieldState,
    toUserJson,
    toNoteJson
  };
})();

// Attach to window for global access
window.VanishValidation = VanishValidation;
