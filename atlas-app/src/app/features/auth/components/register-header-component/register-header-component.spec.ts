import { ComponentFixture, TestBed } from '@angular/core/testing';

import { RegisterHeaderComponent } from './register-header-component';

describe('RegisterHeaderComponent', () => {
  let component: RegisterHeaderComponent;
  let fixture: ComponentFixture<RegisterHeaderComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [RegisterHeaderComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(RegisterHeaderComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the brand copy', () => {
    const compiled = fixture.nativeElement as HTMLElement;
    expect(compiled.querySelector('h1')?.textContent).toContain('Atlas CRM');
    expect(compiled.querySelector('.quote')?.textContent).toContain('Professional client management made simple.');
  });
});
