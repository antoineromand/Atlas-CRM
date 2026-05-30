import { ComponentFixture, TestBed } from '@angular/core/testing';

import { IncomeReportCardComponent } from './income-report-card.component';

describe('IncomeReportCardComponent', () => {
  let component: IncomeReportCardComponent;
  let fixture: ComponentFixture<IncomeReportCardComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [IncomeReportCardComponent],
    }).compileComponents();

    fixture = TestBed.createComponent(IncomeReportCardComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
