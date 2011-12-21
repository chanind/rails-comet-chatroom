module ApplicationHelper
  
  def page_title
    @title || "#{controller_name} - #{action_name}" 
  end
  
end
