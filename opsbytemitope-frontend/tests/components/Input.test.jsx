import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Input from '../../src/components/common/Input';

describe('Input', () => {
  it('renders label when provided', () => {
    render(<Input id="test" label="Email" />);
    expect(screen.getByText('Email')).toBeInTheDocument();
  });

  it('renders placeholder text', () => {
    render(<Input id="test" placeholder="Enter email" />);
    expect(screen.getByPlaceholderText('Enter email')).toBeInTheDocument();
  });

  it('shows error message when error prop is set', () => {
    render(<Input id="test" error="This field is required" />);
    expect(screen.getByText('This field is required')).toBeInTheDocument();
  });

  it('calls onChange handler', () => {
    const handler = vi.fn();
    render(<Input id="test" onChange={handler} />);
    fireEvent.change(screen.getByRole('textbox'), { target: { value: 'test@example.com' } });
    expect(handler).toHaveBeenCalled();
  });

  it('renders required asterisk when required prop is set', () => {
    render(<Input id="test" label="Name" required />);
    expect(screen.getByText('*')).toBeInTheDocument();
  });
});
