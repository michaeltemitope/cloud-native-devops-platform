import { describe, it, expect } from 'vitest';
import { render, screen } from '@testing-library/react';
import { MemoryRouter } from 'react-router-dom';
import NotFoundPage from '../../src/pages/NotFoundPage';

describe('NotFoundPage', () => {
  function renderPage() {
    return render(
      <MemoryRouter>
        <NotFoundPage />
      </MemoryRouter>
    );
  }

  it('renders 404 code', () => {
    renderPage();
    expect(screen.getByText('404')).toBeInTheDocument();
  });

  it('renders the page not found heading', () => {
    renderPage();
    expect(screen.getByRole('heading', { name: /page not found/i })).toBeInTheDocument();
  });

  it('renders a link back to dashboard', () => {
    renderPage();
    const link = screen.getByRole('link', { name: /back to dashboard/i });
    expect(link).toBeInTheDocument();
    expect(link).toHaveAttribute('href', '/dashboard');
  });
});
