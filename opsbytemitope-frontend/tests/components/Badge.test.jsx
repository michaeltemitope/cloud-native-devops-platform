import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import Badge from '../../src/components/common/Badge';

describe('Badge', () => {
  it('renders label text', () => {
    render(<Badge>In Progress</Badge>);
    expect(screen.getByText('In Progress')).toBeInTheDocument();
  });

  it('applies the correct variant class', () => {
    const { container } = render(<Badge variant="success">Done</Badge>);
    expect(container.firstChild.className).toMatch(/success/);
  });

  it('applies default variant when none provided', () => {
    const { container } = render(<Badge>Default</Badge>);
    expect(container.firstChild.className).toMatch(/default/);
  });

  it('renders with sm size', () => {
    const { container } = render(<Badge size="sm">Small</Badge>);
    expect(container.firstChild.className).toMatch(/sm/);
  });
});
