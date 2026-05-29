import { Component, inject, signal } from '@angular/core';
import { Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../../core/services/auth/auth.service';
import { AuthStateService } from '../../core/services/auth/auth-state.service';

interface DashboardNavItem {
  label: string;
  icon: string;
  route?: string;
  disabled?: boolean;
}

@Component({
  selector: 'app-dashboard-layout',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, RouterOutlet],
  templateUrl: './dashboard-layout.html',
  styleUrl: './dashboard-layout.scss',
})
export class DashboardLayoutComponent {
  protected readonly menuOpen = signal(false);
  protected readonly sidebarItems: DashboardNavItem[] = [
    { label: 'Dashboard', icon: 'dashboard', route: '/dashboard' },
    { label: 'Clients', icon: 'group', disabled: true },
    { label: 'Invoices/Quotes', icon: 'receipt_long', disabled: true },
    { label: 'Missions', icon: 'assignment', disabled: true },
  ];

  private readonly authService = inject(AuthService);
  private readonly authStateService = inject(AuthStateService);
  private readonly router = inject(Router);

  closeMenu(): void {
    this.menuOpen.set(false);
  }

  toggleMenu(): void {
    this.menuOpen.update((current) => !current);
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.finishLogout(),
      error: () => this.finishLogout(),
    });
  }

  private finishLogout(): void {
    this.authStateService.clear();
    void this.router.navigate(['/login']);
  }
}
