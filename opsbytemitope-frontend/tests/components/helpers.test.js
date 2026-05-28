import { describe, it, expect } from 'vitest';
import { formatDate, getInitials, getErrorMessage, STATUS_CONFIG, PRIORITY_CONFIG } from '../../src/utils/helpers';

describe('formatDate', () => {
  it('returns em dash for null/undefined', () => {
    expect(formatDate(null)).toBe('—');
    expect(formatDate(undefined)).toBe('—');
  });

  it('formats a valid ISO date string', () => {
    const result = formatDate('2024-03-15T00:00:00.000Z');
    expect(result).toMatch(/Mar/);
    expect(result).toMatch(/2024/);
  });
});

describe('getInitials', () => {
  it('returns initials from a two-word name', () => {
    expect(getInitials('Jane Smith')).toBe('JS');
  });

  it('returns single initial for a single word', () => {
    expect(getInitials('Jane')).toBe('J');
  });

  it('returns empty string for empty input', () => {
    expect(getInitials('')).toBe('');
  });

  it('uppercases initials', () => {
    expect(getInitials('john doe')).toBe('JD');
  });

  it('only uses first two words', () => {
    expect(getInitials('John Michael Doe')).toBe('JM');
  });
});

describe('getErrorMessage', () => {
  it('returns string errors as-is', () => {
    expect(getErrorMessage('Something broke')).toBe('Something broke');
  });

  it('extracts message from axios response', () => {
    const err = { response: { data: { message: 'Not found' } } };
    expect(getErrorMessage(err)).toBe('Not found');
  });

  it('falls back to error.message', () => {
    const err = { message: 'Network error' };
    expect(getErrorMessage(err)).toBe('Network error');
  });

  it('returns fallback string for unknown error shape', () => {
    expect(getErrorMessage({})).toBe('Something went wrong. Please try again.');
  });
});

describe('STATUS_CONFIG', () => {
  it('contains expected status keys', () => {
    expect(STATUS_CONFIG).toHaveProperty('todo');
    expect(STATUS_CONFIG).toHaveProperty('in_progress');
    expect(STATUS_CONFIG).toHaveProperty('done');
    expect(STATUS_CONFIG).toHaveProperty('blocked');
  });

  it('each entry has label and variant', () => {
    Object.values(STATUS_CONFIG).forEach(cfg => {
      expect(cfg).toHaveProperty('label');
      expect(cfg).toHaveProperty('variant');
    });
  });
});

describe('PRIORITY_CONFIG', () => {
  it('contains low, medium, high', () => {
    expect(PRIORITY_CONFIG).toHaveProperty('low');
    expect(PRIORITY_CONFIG).toHaveProperty('medium');
    expect(PRIORITY_CONFIG).toHaveProperty('high');
  });
});
