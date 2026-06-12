import { Component, EventEmitter, Input, Output } from '@angular/core';

export interface PaginationBarState {
  page: number;
  totalElements: number;
  totalPages: number;
  hasNext: boolean;
  hasPrevious: boolean;
}

@Component({
  selector: 'app-pagination-bar',
  standalone: true,
  templateUrl: './pagination-bar.component.html',
  styleUrl: './pagination-bar.component.scss',
})
export class PaginationBarComponent {
  @Input() pagination: PaginationBarState | null = null;
  @Input() itemCount = 0;
  @Input() collectionLabel = 'items';
  @Input() ariaLabel = 'Pagination';
  @Input() isRefreshing = false;

  @Output() previous = new EventEmitter<void>();
  @Output() next = new EventEmitter<void>();
}
