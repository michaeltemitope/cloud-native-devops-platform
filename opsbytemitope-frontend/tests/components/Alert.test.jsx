import { describe, it, expect, vi } from 'vitest';
import { render, screen, fireEvent } from '@testing-library/react';
import Alert from '../../src/components/common/Alert';

describe('Alert', () => {
  it('renders message text', () => {
    render(<Alert>Something went wrong</Alert>);
    expect(screen.getByText('Something went wrong')).toBeInTheDocument();
  });

  it('renders nothing when children is empty', () => {
    const { container } = render(<Alert>{''}</Alert>);
    expect(container.firstChild).toBeNull();
  });

  it('shows dismiss button when onDismiss is provided', () => {
    render(<Alert onDismiss={() => {}}>Error message</Alert>);
    expect(screen.getByRole('button', { name: /dismiss/i })).toBeInTheDocument();
  });

  it('calls onDismiss when dismiss button clicked', () => {
    const handler = vi.fn();
    render(<Alert onDismiss={handler}>Error</Alert>);
    fireEvent.click(screen.getByRole('button', { name: /dismiss/i }));
    expect(handler).toHaveBeenCalledTimes(1);
  });

  it('does not show dismiss button without onDismiss prop', () => {
    render(<Alert>Error</Alert>);
    expect(screen.queryByRole('button')).not.toBeInTheDocument();
  });

  it('applies error type class by default', () => {
    const { container } = render(<Alert>Error</Alert>);
    expect(container.firstChild.className).toMatch(/error/);
  });

  it('applies success type class', () => {
    const { container } = render(<Alert type="success">Success</Alert>);
    expect(container.firstChild.className).toMatch(/success/);
  });
});
