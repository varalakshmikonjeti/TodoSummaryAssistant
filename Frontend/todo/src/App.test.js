jest.mock('./services/todoService', () => ({
  __esModule: true,
  default: {
    getAllTodos: () => Promise.resolve({ data: [] }),
    createTodo: () => Promise.resolve({ data: {} }),
    updateTodo: () => Promise.resolve({ data: {} }),
    deleteTodo: () => Promise.resolve(),
    summarizeTodos: () => Promise.resolve({ data: 'Summary sent successfully.' })
  }
}));

import React from 'react';
import { render, screen, waitFor } from '@testing-library/react';
import App from './App';

test('renders Todo Summary Assistant', async () => {
  render(<App />);

  expect(
    screen.getByRole('heading', { name: /todo summary assistant/i })
  ).toBeInTheDocument();

  expect(
    screen.getByRole('button', {
      name: /summarize pending todos & send to slack/i
    })
  ).toBeInTheDocument();

  await waitFor(() => {
    expect(
      screen.getByRole('heading', { name: /todo summary assistant/i })
    ).toBeInTheDocument();
  });
});
